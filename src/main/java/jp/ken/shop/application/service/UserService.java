package jp.ken.shop.application.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.ken.shop.domain.entity.RegisterEntity;
import jp.ken.shop.domain.repository.UserRepository;
import jp.ken.shop.presentation.form.UserForm;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public RegisterEntity register(UserForm form) {

        RegisterEntity entity = new RegisterEntity();

        // --- 必須項目 ---
        entity.setMemberName(form.getName());
        entity.setMemberKana(form.getNameKana());
        entity.setMemberMail(form.getEmail());
        entity.setMemberPost(form.getPostcode());
        entity.setMemberAddress(form.getAddress());
        entity.setMemberPhone(form.getTelephonenumber());
        entity.setMemberPassword(form.getPassword());

        // 生年月日(yyyyMMdd) → LocalDate
        entity.setMemberBirthday(parseYyyyMmDd(form.getBirthDate()));

        // 支払方法（1=クレカ、2=現金）
        String paymentCode = toPaymentCode(form.getPayment());
        entity.setMemberPayment(paymentCode);

        // クレジットカード情報（クレカ選択時のみ）
        if ("1".equals(paymentCode)) {
            entity.setCreditType(emptyToNull(form.getCreditType()));
            entity.setCreditNumber(emptyToNull(form.getCreditNumber()));
            entity.setCreditName(emptyToNull(form.getCreditName()));
            entity.setCreditSecurityNumber(emptyToNull(form.getCreditSecurityNumber()));
        } else {
            entity.setCreditType(null);
            entity.setCreditNumber(null);
            entity.setCreditName(null);
            entity.setCreditSecurityNumber(null);
        }

        // 初期値
        entity.setMemberPoint(0);
        entity.setSignupDay(LocalDate.now());
        entity.setValidFlag("1");

        // ★ INSERTしてAUTO_INCREMENTのIDを取得
        int memberId = userRepository.insert(entity);
        entity.setMemberId(memberId);

        // ★ Controller に返す
        return entity;
    }

    // 支払方法コード変換
    private static String toPaymentCode(String payment) {
        if (payment == null || payment.isBlank()) return "1";
        String p = payment.trim();
        if ("1".equals(p) || "2".equals(p)) return p;
        if (p.equalsIgnoreCase("CARD") || p.contains("クレジット")) return "1";
        if (p.equalsIgnoreCase("CASH") || p.contains("現金")) return "2";
        return "1";
    }

    // yyyyMMdd → LocalDate
    private static LocalDate parseYyyyMmDd(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.isBlank()) {
            throw new IllegalArgumentException("生年月日が未入力です");
        }
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("uuuuMMdd");
            return LocalDate.parse(yyyymmdd.trim(), fmt);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("生年月日が不正です（yyyyMMdd）: " + yyyymmdd, e);
        }
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
