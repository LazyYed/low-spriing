package com.lazy.utils;

/**
 * @author lazyyedi@gamil.com
 * @creed: 我不能创造的东西，我就无法理解
 * @date 2021/10/16 下午10:08
 */
public class CommonUtil {
    /**
     * 首字母小写
     * @param str
     * @return
     */
    public static String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    // 把String类型转变成其他类型
    public static Object string2Other(String clientValue, Class<?> parameterType){
        if(String.class==parameterType){
            return clientValue;
        }
        if(Integer.class==parameterType){
            return Integer.valueOf(clientValue);
        }else if(Double.class==parameterType){
            return Double.valueOf(clientValue);
        }else{
            return null;
        }
    }
}
