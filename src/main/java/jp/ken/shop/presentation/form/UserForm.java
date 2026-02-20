package jp.ken.shop.presentation.form;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jp.ken.shop.common.validator.groups.ValidGroup1;
import jp.ken.shop.common.validator.groups.ValidGroup2;
import lombok.Data;

@Data
public class UserForm implements Serializable {

    @NotBlank(message = "名前は必須です。", groups = ValidGroup1.class)
    @Pattern(regexp = "^[^\\s]+$", message = "名前はスペースなしで入力してください。", groups = ValidGroup2.class)
    private String name;

    @NotBlank(message = "名前(カナ)は必須です。", groups = ValidGroup1.class)
    @Pattern(regexp = "^[ァ-ヶー]+$", message = "カタカナで入力してください。", groups = ValidGroup2.class)
    private String nameKana;

    @NotBlank(message = "メールアドレスは必須です。", groups = ValidGroup1.class)
    private String email;

    @NotBlank(message = "郵便番号は必須です。", groups = ValidGroup1.class)
    @Pattern(regexp = "^\\d{7}$", message = "郵便番号は7桁の数字で入力してください。", groups = ValidGroup2.class)
    private String postcode;

    @NotBlank(message = "住所は必須です。", groups = ValidGroup1.class)
    private String address;

    @NotBlank(message = "電話番号は必須です。", groups = ValidGroup1.class)
    @Pattern(regexp = "^\\d{11}$", message = "電話番号は11桁の数字で入力してください。", groups = ValidGroup2.class)
    private String telephonenumber;

    @NotBlank(message = "生年月日は必須です。", groups = ValidGroup1.class)
    @Pattern(regexp = "^\\d{8}$", message = "生年月日は8桁(YYYYMMDD)で入力してください。", groups = ValidGroup2.class)
    private String birthDate;

    @NotBlank(message = "パスワードは必須です。", groups = ValidGroup1.class)
    @Size(min = 10, message = "パスワードは10文字以上で入力してください。", groups = ValidGroup2.class)
    private String password;

    @NotBlank(message = "確認パスワードは必須です。", groups = ValidGroup1.class)
    private String confirmPassword;

    // 初期値：クレジットカード
    @NotBlank(message = "必須入力です。", groups = ValidGroup1.class)
    private String payment;
    
    private int memderId;
    private String creditType;
    private String creditNumber;
    private String creditName;
    private String creditSecurityNumber;
    private String LoginInput;
}
