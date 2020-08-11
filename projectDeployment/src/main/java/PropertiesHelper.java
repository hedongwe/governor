import java.util.ResourceBundle;

public class PropertiesHelper {
     private ResourceBundle resourceBundle;

     public PropertiesHelper(){
         super();
         init();
     }
    public PropertiesHelper(String fileName){
        this.resourceBundle = ResourceBundle.getBundle(fileName);
    }

    private void init(){
         this.resourceBundle = ResourceBundle.getBundle("serverInfo");
     }

    public static String getValue(String key){
         PropertiesHelper ph = new PropertiesHelper();
         return ph.resourceBundle.containsKey(key)?ph.resourceBundle.getString(key):"";
     }
}
