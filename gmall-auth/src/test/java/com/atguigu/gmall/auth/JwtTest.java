package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\project\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MDk2MDI0NzB9.P90-LGlJ4z2qSArDP308zol-fYY_0bvyLe2a0D8LnBDySJZpL0c8OalJtXLKsCF__T-rjnvPLGGNQ4adlHfuOtuyJP0qM8m7-d7oWDTLUOfyrhh3zjE_xWMvBrXppcW2GCMW9nsdqN_FMQ-K9v37ynU7FITD6HItpidUdWr-qEKb-k9o6cDr3bUwbo1XW_6ur11z-zM7yNN1GuWb2SvCm2A_VF2dS67hpLjxtJhgnmxxFIU_hyGqBgyl2tQYvt5UigLA_0oUnKtb7TG4VGPeNc5Fy9oE2KoKtp1XJfdbsmd9vFz6rZ1edFx_6geJMUJmeScI0XWn4M3SINfqKUHXeA";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}