package top.swjtuhc.accounting_management_api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
public class PasswordEncoder {

    public static String encode(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("原始密码不能为空");
        }

        // 加密过程
        String salt = UUID.randomUUID().toString().replace("-", "");
        String encodedPassword = rawPassword;
        for (int i = 0; i < 5; i++) {
            encodedPassword = DigestUtils.md5DigestAsHex(
                    (salt + encodedPassword + salt + encodedPassword + salt).getBytes());
        }
        return salt + encodedPassword;
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            return false;
        }

        // 如果是未加密的密码（长度小于32），直接比较
        if (encodedPassword.length() < 32) {
            return encodedPassword.equals(rawPassword);
        }

        try {
            String salt = encodedPassword.substring(0, 32);
            String newPassword = rawPassword;
            for (int i = 0; i < 5; i++) {
                newPassword = DigestUtils.md5DigestAsHex(
                        (salt + newPassword + salt + newPassword + salt).getBytes());
            }
            newPassword = salt + newPassword;
            return newPassword.equals(encodedPassword);
        } catch (Exception e) {
            log.error("密码匹配异常", e);
            return false;
        }
    }
}