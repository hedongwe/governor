import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import sun.net.ftp.FtpClient;

import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class SSH {
    private String username = null;
    private String password = null;
    private String host = null;
    private int port = 0;
    private Session session =null;
    private ArrayList<String> stdout;


    public SSH(){
        this.host = PropertiesHelper.getValue("host");
        this.username = PropertiesHelper.getValue("username");
        this.password = PropertiesHelper.getValue("password");
        this.port = Integer.valueOf(PropertiesHelper.getValue("port"));
        this.stdout = new ArrayList<String>();


        session = getConnectSession();
    }

    public SSH(String host,int port,String username,String password){
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.stdout = new ArrayList<String>();

        session = getConnectSession();
    }

//    public void ftpClientInit() {
//        try {
//            this.ftpClient.connect(this.host, 21);
//System.out.println(host+";;;;;;;;;;;;;;");
//            int reply = this.ftpClient.getReplyCode();
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                this.ftpClient.disconnect();
//            }
//
//            if (!this.ftpClient.login(this.username, this.password)) {
//                this.ftpClient.disconnect();
//            }
//
//            // set data transfer mode.
//            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//            // Use passive mode to pass firewalls.
//            this.ftpClient.enterLocalPassiveMode();
//
//        } catch (Exception e) {
//            this.ftpClient = null;
//        }
//    }

    public Session getSession() {
        return session;
    }

    //连接远程服务器
    private Session getConnectSession(){
        int returnCode = 0;
        JSch jsch = new JSch();
        //创建session并且打开连接，因为创建session之后要主动打开连接
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect(3000);//连接3秒超时
        } catch (JSchException e) {
            e.printStackTrace();
            return null;
        }
        return session;
    }

    //上传文件到远程服务器指定目录
    public short uploadFile(File file,String targetDir) {
        ChannelSftp sftp = null;
        Channel channel = null;

        short retCode = 0;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            InputStream ins = new FileInputStream(file);

            sftp.cd(targetDir);
            //中文名称的
            sftp.put(ins, new String(file.getName().getBytes(),"UTF-8"));

        } catch (JSchException jse){
            retCode = -1;
            jse.printStackTrace();
        } catch(Exception e) {
            retCode = -1;
            e.printStackTrace();
        }finally {
            closeChannel(sftp);
            closeChannel(channel);
        }
        return retCode;
    }


    //上传文件到远程服务器指定目录
    public short uploadFile(InputStream ins,String targetDir,String fileName) {
        ChannelSftp sftp = null;
        Channel channel = null;
        short retCode = 0;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            sftp.cd(targetDir);
            //中文名称的
            sftp.put(ins, new String(fileName.getBytes(),"UTF-8"));

        } catch (JSchException jse){
            retCode = -1;
            jse.printStackTrace();
        } catch(Exception e) {
            retCode = -1;
            e.printStackTrace();
        }finally {
            closeChannel(sftp);
            closeChannel(channel);
        }
        return retCode;
    }

    //执行远程服务器shell脚本命令
    public int execShell(String command) {
        Channel channel = null;
        ChannelSftp sftp = null;
        int retCode = 0;
        try {
            channel = session.openChannel("exec");

            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            BufferedReader input = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            channel.connect();

            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
            }
            input.close();
            //输出shell脚本执行结果
            for(Object a : stdout){
                 System.out.println(a.toString());
            }

            if (channel.isClosed()) {
                retCode = channel.getExitStatus();
            }
        }  catch (JSchException jse){
            retCode = -1;
            jse.printStackTrace();
        } catch(Exception e) {
            retCode = -1;
            e.printStackTrace();
        }finally {
            closeChannel(channel);
        }
        return retCode;
    }


    //ssh连接linux服务器列出远程服务器目录下的内容
    public void listFileNames(String targetDir) {
        ChannelSftp sftp = null;
        Channel channel = null;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            //查看目录下的文件
            Vector<?> vector = sftp.ls(targetDir);
            for (Object item:vector) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) item;
                System.out.println(entry.getFilename());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeChannel(sftp);
            closeChannel(channel);
        }
    }

    //ssh连接linux服务器 下载文件
//    public InputStream downloadFile(String filePath,String fileName) {
//        InputStream is = null;
//        try {
//            channel = session.openChannel("sftp");
//            channel.connect();
//            sftp = (ChannelSftp) channel;
//            sftp.cd(filePath);
//            is = sftp.get(fileName);
//
////            // 判断该目录下是否有文件
////            if (fs == null || fs.length == 0) {
////                System.out.println(BASE_PATH + ftpPath + "该目录下没有文件");
////                LOGGER.error(BASE_PATH + ftpPath + "该目录下没有文件");
////                return input;
////            }
//            for (String ff : fs) {
//                String ftpName = new String(ff.getBytes("ISO-8859-1"), "UTF-8");
//                if (ftpName.equals(fileName)) {
//                    is = ftpClient.retrieveFileStream(ff);
//                    break;
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            try {
//                is.close();
//                ftpClient.logout();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//
//        }
//        return is;
//    }



    private static void closeChannel(Channel channel) {
        if (channel != null) {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    public void closeSession() {
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public static void main(String[] args){
//        SSH  ssh = new SSH( "139.9.151.109", 22,"root", "Dota2010huaweiyun");
//        ssh.downloadFile("/opt/tomcat8","run.log","C:\\Users\\hdw\\Desktop");
          //System.out.println("mom_main.zip".substring(0,"mom_main.zip".indexOf(".")));
//        String host = "139.9.151.109",username = "root", password = "Dota2010huaweiyun", targetDir =  "/opt/test";
//        File file = new File("C:\\Users\\hdw\\Desktop\\a.zip");
//        SSH ssh = new SSH();
//        ssh.uploadFile(file,targetDir);
//        ssh.listFileNames(targetDir);
//        int a = ssh.execShell("sh /opt/test/t.sh a");
//        System.out.println("^^^^^^^^^^^^^^^^^^^^^" + a);
//        ssh.listFileNames(targetDir);
    }

}
