package lightning.utils.miscellaneous;

import java.util.List;

public class ListUtils {
    public static int getIndex(List arg0, String arg1) {
        int var2 = 0;
        while (var2 < arg0.size()) {
            if (!(((String) arg0.get(var2)).equalsIgnoreCase(arg1))) {
            } else {
                return var2;
            }
            var2++;
        }
        return -1;
    }

}
