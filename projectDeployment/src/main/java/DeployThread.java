import java.util.concurrent.Callable;

public class DeployThread implements Callable<String> {
    private String serverIp;
    private String fileName;
    private SSH   ssh;
    private int exectueResult = -1;

    public DeployThread(String serverIp,String fileName){
        this.fileName = fileName;
        this.serverIp = serverIp;
        String username=PropertiesHelper.getValue("username");
        String password=PropertiesHelper.getValue("password");
        int port = Integer.valueOf(PropertiesHelper.getValue("port"));
        String targetDir=PropertiesHelper.getValue("targetDir");
        this.ssh = new SSH(serverIp, port, username, password);

    }

    public String call() throws Exception {
        String command = "sh "+PropertiesHelper.getValue("tcommand");
        if(this.fileName.indexOf(".war.") > -1){
            command = "sh "+PropertiesHelper.getValue("jcommand");
        }
        this.exectueResult = this.ssh.execShell(command+" "+this.fileName.substring(0,this.fileName.indexOf(".")));//执行远程shell脚本
        this.ssh.closeSession();
        return this.serverIp;
    }
}
