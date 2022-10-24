package github.javaguide.utils;

/**
 * @author zhp
 * @date 2022-10-24 17:13
 */
public class StringUtil {
    /**
     *判断name是否为null或着为空，同时检测是否包含空格
     * @param name
     * @return
     */
    public static boolean isBlank(String name) {
        if(name==null||name.length()==0){
            return true;
        }

        for(int i=0;i<name.length();i++){
            if(Character.isWhitespace(name.charAt(i))){
                return true;
            }
        }
        return false;
    }
}
