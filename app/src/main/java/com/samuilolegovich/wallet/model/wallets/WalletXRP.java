package com.samuilolegovich.wallet.model.wallets;

import com.google.common.primitives.UnsignedInteger;
import com.samuilolegovich.wallet.model.wallets.interfaces.MyWallets;
import com.samuilolegovich.wallet.myClient.MyXrplClient;

import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Memo;
import org.xrpl.xrpl4j.model.transactions.MemoWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XAddress;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * Низкоуровневая реализация кошелька XRPL: генерация/восстановление ключевой пары
 * из сид-фразы, запрос баланса и сиквенса счёта через RPC, формирование, подпись
 * и отправка платёжных транзакций (обычных, с мемо или с destination tag).
 */
public class WalletXRP implements MyWallets {
    private AccountInfoRequestParams requestParams;
    private AccountInfoResult accountInfoResult;
    private UnsignedInteger lastLedgerSequence;
    private UnsignedInteger sequence;
    private Address classicAddress;
    private MyXrplClient xrplClient;
    private KeyPair keyPair;
    private Seed seed;
    private String seedBase58;

    private SingleSignedTransaction<Payment> signedPayment;
    private Map<String, String> createNewWalletData;



    /** Создаёт пустую обёртку кошелька; реальная инициализация происходит в {@link #createNewWallet()} или {@link #restoreWallet(String)}. */
    public WalletXRP() {}



    // ---- методы совместимости (используются из PaymentAndSocketManagerXRPL) ----

    /** Возвращает классический XRPL-адрес текущего кошелька. */
    public Address classicAddress() {
        return classicAddress;
    }

    /** Возвращает приватный ключ кошелька в шестнадцатеричном виде. */
    public Optional<String> privateKey() {
        return Optional.of(keyPair.privateKey().prefixedBytes().hexValue());
    }

    /** Возвращает публичный ключ кошелька в шестнадцатеричном виде. */
    public String publicKey() {
        return keyPair.publicKey().base16Value();
    }

    /** Преобразует классический адрес в X-адрес. */
    public XAddress xAddress() {
        return AddressCodec.getInstance().classicAddressToXAddress(classicAddress, false);
    }

    /** Сообщает, является ли этот кошелёк тестовым (для данной реализации — всегда false). */
    public boolean isTest() {
        return false;
    }

    // -------------------------------------------------------------------------



    /** Запрашивает свежую информацию о счёте и возвращает текущий баланс в XRP (0, если счёт ещё не активирован). */
    @Override
    public BigDecimal getBalance() {
        getInformationAboutYourAccount();
        return accountInfoResult != null
                ? accountInfoResult.accountData().balance().toXrp()
                : BigDecimal.ZERO;
    }

    /** Возвращает сид-фразу только что созданного кошелька; для восстановленного кошелька сид недоступен из соображений безопасности. */
    @Override
    public String getSeed() {
        if (createNewWalletData != null) {
            return seedBase58;
        }
        return "Это не новый кошелек, у вас уже есть востановительная фраза";
    }


    /** Генерирует новый ed25519-кошелёк: сид, ключевую пару и адрес, сохраняет сид в настройках и возвращает все ключевые данные кошелька. */
    @Override
    public Map<String, String> createNewWallet() {
        seed = Seed.ed25519Seed();
        keyPair = seed.deriveKeyPair();
        classicAddress = keyPair.publicKey().deriveAddress();

        Decoded decoded = seed.decodedSeed();
        seedBase58 = AddressBase58.encode(
                decoded.bytes(),
                Collections.singletonList(decoded.version()),
                UnsignedInteger.valueOf(16));

        createConnect();
        getInformationAboutYourAccount();

        createNewWalletData = Map.of(
                "Seed", seedBase58,
                "Public Key", keyPair.publicKey().base16Value(),
                "Private Key", keyPair.privateKey().prefixedBytes().hexValue(),
                "Classic Address", classicAddress.toString(),
                "Balance", accountInfoResult != null
                        ? accountInfoResult.accountData().balance().toString()
                        : "0000000");
        return new HashMap<>(createNewWalletData);
    }


    /** Восстанавливает кошелёк из переданной сид-фразы: выводит ключевую пару и адрес, запрашивает баланс. Возвращает пустую карту при ошибке (например, неверный формат сида). */
    public Map<String, String> restoreWallet(String seedStr) {
        try {
            seedBase58 = seedStr;
            seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of(seedStr));
            keyPair = seed.deriveKeyPair();
            classicAddress = keyPair.publicKey().deriveAddress();

            createConnect();
            getInformationAboutYourAccount();

            return Map.of(
                    "Public Key", keyPair.publicKey().base16Value(),
                    "Private Key", keyPair.privateKey().prefixedBytes().hexValue(),
                    "Classic Address", classicAddress.toString(),
                    "Balance", accountInfoResult != null
                            ? accountInfoResult.accountData().balance().toString()
                            : "0000000");
        } catch (Exception e) {
            return new HashMap<>();
        }
    }


    /** Формирует, подписывает и отправляет платёж с числовым destination tag (например, для платежей на биржу). */
    public boolean sendPaymentToAddressXRP(String address, Integer tag, BigDecimal numberOfXRP) {
        try {
            createConnect();
            getInformationAboutYourAccount();

            FeeResult feeResult = xrplClient.fee();
            XrpCurrencyAmount openLedgerFee = feeResult.drops().openLedgerFee();

            LedgerIndex validatedLedger = xrplClient.ledger(LedgerRequestParams.builder()
                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                    .build())
                    .ledgerIndex()
                    .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

            lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue();

            Payment payment = Payment.builder()
                    .account(classicAddress)
                    .amount(XrpCurrencyAmount.ofXrp(numberOfXRP))
                    .destination(Address.of(address))
                    .destinationTag(UnsignedInteger.valueOf(tag))
                    .sequence(sequence)
                    .fee(openLedgerFee)
                    .signingPublicKey(keyPair.publicKey())
                    .lastLedgerSequence(lastLedgerSequence)
                    .build();

            BcSignatureService signatureService = new BcSignatureService();
            signedPayment = signatureService.sign(keyPair.privateKey(), payment);

            SubmitResult<Payment> prelimResult = xrplClient.submit(signedPayment);
            System.out.println("Submit Result Transaction:  -- >  " + prelimResult);
        } catch (NullPointerException | JsonRpcClientErrorException e) {
            return false;
        }
        return true;
    }

    /** Формирует, подписывает и отправляет платёж с мемо в шестнадцатеричной кодировке (используется для ставок в играх, кодирующих параметры ставки в тексте мемо). */
    public boolean sendPaymentToAddressXRP(String address, String memo, BigDecimal numberOfXRP) {
        try {
            createConnect();
            getInformationAboutYourAccount();

            FeeResult feeResult = xrplClient.fee();
            XrpCurrencyAmount openLedgerFee = feeResult.drops().openLedgerFee();

            LedgerIndex validatedLedger = xrplClient.ledger(LedgerRequestParams.builder()
                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                    .build())
                    .ledgerIndex()
                    .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

            lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue();

            MemoWrapper memoWrapper = MemoWrapper.builder()
                    .memo(Memo.builder()
                            .memoData(toHex(memo))
                            .build())
                    .build();

            Payment payment = Payment.builder()
                    .account(classicAddress)
                    .amount(XrpCurrencyAmount.ofXrp(numberOfXRP))
                    .destination(Address.of(address))
                    .sequence(sequence)
                    .fee(openLedgerFee)
                    .signingPublicKey(keyPair.publicKey())
                    .lastLedgerSequence(lastLedgerSequence)
                    .memos(List.of(memoWrapper))
                    .build();

            BcSignatureService signatureService = new BcSignatureService();
            signedPayment = signatureService.sign(keyPair.privateKey(), payment);

            SubmitResult<Payment> prelimResult = xrplClient.submit(signedPayment);
            System.out.println("Submit Result Transaction:  -- >  " + prelimResult);
        } catch (NullPointerException | JsonRpcClientErrorException e) {
            return false;
        }
        return true;
    }

    /** Формирует, подписывает и отправляет обычный платёж без мемо и тега (стандартный перевод XRP на другой адрес). */
    public boolean sendPaymentToAddressXRP(String address, BigDecimal numberOfXRP) {
        try {
            createConnect();
            getInformationAboutYourAccount();

            FeeResult feeResult = xrplClient.fee();
            XrpCurrencyAmount openLedgerFee = feeResult.drops().openLedgerFee();

            LedgerIndex validatedLedger = xrplClient.ledger(LedgerRequestParams.builder()
                            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                            .build())
                    .ledgerIndex()
                    .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

            lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue();

            Payment payment = Payment.builder()
                    .account(classicAddress)
                    .amount(XrpCurrencyAmount.ofXrp(numberOfXRP))
                    .destination(Address.of(address))
                    .sequence(sequence)
                    .fee(openLedgerFee)
                    .signingPublicKey(keyPair.publicKey())
                    .lastLedgerSequence(lastLedgerSequence)
                    .build();

            BcSignatureService signatureService = new BcSignatureService();
            signedPayment = signatureService.sign(keyPair.privateKey(), payment);

            SubmitResult<Payment> prelimResult = xrplClient.submit(signedPayment);
            System.out.println("Submit Result Transaction:  -- >  " + prelimResult);
        } catch (NullPointerException | JsonRpcClientErrorException e) {
            return false;
        }
        return true;
    }


    /** Опрашивает леджер до тех пор, пока отправленная транзакция не будет подтверждена либо не истечёт её lastLedgerSequence. */
    private void waitForValidationTransaction() {
        try {
            boolean transactionValidated = false;
            boolean transactionExpired = false;

            while (!transactionValidated && !transactionExpired) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                LedgerIndex latestValidatedLedgerIndex = xrplClient.ledger(LedgerRequestParams.builder()
                                .ledgerSpecifier(LedgerSpecifier.VALIDATED).build())
                        .ledgerIndex()
                        .orElseThrow(() -> new RuntimeException("Ledger response did not contain a LedgerIndex."));

                TransactionResult<Payment> transactionResult = xrplClient.transaction(
                        TransactionRequestParams.of(signedPayment.hash()), Payment.class
                );

                if (transactionResult.validated()) {
                    System.out.println("Payment was validated with result code:  -- >  "
                            + transactionResult.metadata().get().transactionResult());
                    transactionValidated = true;
                } else {
                    boolean lastLedgerSequenceHasPassed = FluentCompareTo
                            .is(latestValidatedLedgerIndex.unsignedIntegerValue())
                            .greaterThan(lastLedgerSequence);
                    if (lastLedgerSequenceHasPassed) {
                        System.out.println("LastLedgerSequence has passed. Last tx response:  -- >  " + transactionResult);
                        transactionExpired = true;
                        checkTransactionResults(transactionResult, signedPayment);
                    } else {
                        System.out.println("Payment not yet validated.");
                    }
                }
            }
        } catch (JsonRpcClientErrorException e) {
            e.printStackTrace();
        }
    }


    /** Выводит в лог итоговый результат истёкшей транзакции (код результата и доставленную сумму) и ссылку на explorer. */
    private void checkTransactionResults(TransactionResult<Payment> transactionResult,
                                         SingleSignedTransaction<Payment> signedPayment) {
        AtomicBoolean flag = new AtomicBoolean(true);
        while (flag.get()) {
            System.out.println("Transaction Result:  -- >  " + transactionResult);
            System.out.println("Explorer link:  -- >  https://testnet.xrpl.org/transactions/" + signedPayment.hash());

            transactionResult.metadata().ifPresent(metadata -> {
                System.out.println("Result code:  -- >  " + metadata.transactionResult());
                metadata.deliveredAmount().ifPresent(deliveredAmount ->
                        System.out.println("XRP Delivered:  -- >  " + ((XrpCurrencyAmount) deliveredAmount).toXrp()));
                flag.set(false);
            });

            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Преобразует текст в шестнадцатеричную строку для размещения в поле мемо XRPL-транзакции. */
    private static String toHex(String text) {
        StringBuilder sb = new StringBuilder();
        for (byte b : text.getBytes(StandardCharsets.UTF_8)) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /** Создаёт новый RPC-клиент к текущей сети (testnet/mainnet), так как сеть может быть переключена пользователем в любой момент. */
    private void createConnect() {
        try {
            xrplClient = new MyXrplClient(com.samuilolegovich.config.NetworkConfig.getRpcUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Запрашивает у RPC текущую информацию о счёте (баланс, сиквенс); если счёт не активирован, тихо логирует подсказку об этом. */
    private void getInformationAboutYourAccount() {
        try {
            createConnect(); // всегда используем текущую сеть (testnet/mainnet могла быть переключена)
            requestParams = AccountInfoRequestParams.builder()
                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                    .account(classicAddress)
                    .build();

            accountInfoResult = xrplClient.accountInfo(requestParams);
            sequence = accountInfoResult.accountData().sequence();
        } catch (JsonRpcClientErrorException e) {
            System.out.println("Для начала стоило бы активировать счет - пополнить.");
        }
    }
}