import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP服务器工具类 
 *
 */
public class FTPUtils {

    /**
     * 上传文件至FTP服务器
     *
     * @param url
     *   服务器IP地址
     * @param port
     *   服务器端口
     * @param userName
     *   用户登录名
     * @param password
     *   用户登录密码
     * @param storePath
     *   服务器文件存储路径
     * @param fileName
     *   服务器文件存储名称
     * @param is
     *   文件输入流
     * @return
     *   <b>true</b>：上传成功
     *   <br/>
     *   <b>false</b>：上传失败
     */
    public static boolean storeFile (String url, int port, String userName, String password, String storePath, String fileName, InputStream is) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            // 连接至服务器，端口默认为21时，可直接通过URL连接
            ftp.connect(url ,port);
            // 登录服务器
            ftp.login(userName, password);
            // 判断返回码是否合法
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                // 不合法时断开连接
                ftp.disconnect();
                // 结束程序
                return result;
            }
            // 判断ftp目录是否存在，如果不存在则创建目录，包括创建多级目录
            String s = "/"+storePath;
            String[] dirs = s.split("/");
            ftp.changeWorkingDirectory("/");
            //按顺序检查目录是否存在，不存在则创建目录
            for(int i=1; dirs!=null&&i<dirs.length; i++) {
                if(!ftp.changeWorkingDirectory(dirs[i])) {
                    if(ftp.makeDirectory(dirs[i])) {
                        if(!ftp.changeWorkingDirectory(dirs[i])) {
                            return false;
                        }
                    }else {
                        return false;
                    }
                }
            }
            // 设置文件操作目录
            ftp.changeWorkingDirectory(storePath);
            // 设置文件类型，二进制
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 设置缓冲区大小
            ftp.setBufferSize(3072);
            // 上传文件
            result = ftp.storeFile(fileName, is);
            // 关闭输入流
            is.close();
            // 登出服务器
            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 判断输入流是否存在
                if (null != is) {
                    // 关闭输入流
                    is.close();
                }
                // 判断连接是否存在
                if (ftp.isConnected()) {
                    // 断开连接
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 从FTP服务器下载文件至本地
     *
     * @param url
     *   服务器IP地址
     * @param port
     *   服务器端口
     * @param userName
     *   用户登录名
     * @param password
     *   用户登录密码
     * @param remotePath
     *   服务器文件存储路径
     * @param fileName
     *   服务器文件存储名称
     * @param localPath
     *   本地文件存储路径
     * @return
     *   <b>true</b>：下载成功
     *   <br/>
     *   <b>false</b>：下载失败
     */
    public static boolean retrieveFile (String url, int port, String userName, String password, String remotePath, String fileName, String localPath) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        OutputStream os = null;
        try {
            // 连接至服务器，端口默认为21时，可直接通过URL连接
            System.out.println(url);
            System.out.println(port);
            ftp.connect(url ,port);
            // 登录服务器
            ftp.login(userName, password);
            // 判断返回码是否合法
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                // 不合法时断开连接
                ftp.disconnect();
                // 结束程序
                return result;
            }
            // 设置文件操作目录
            ftp.changeWorkingDirectory(remotePath);
            // 设置文件类型，二进制
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 设置缓冲区大小
            ftp.setBufferSize(3072);
            // 设置字符编码
            ftp.setControlEncoding("UTF-8");
            // 构造本地文件对象
            File localFile = new File(localPath + "/" + fileName);
            // 获取文件操作目录下所有文件名称
            String[] remoteNames = ftp.listNames();
            // 循环比对文件名称，判断是否含有当前要下载的文件名
            for (String remoteName: remoteNames) {
                if (fileName.equals(remoteName)) {
                    result = true;
                }
            }
            // 文件名称比对成功时，进入下载流程
            if (result) {
                // 构造文件输出流
                os = new FileOutputStream(localFile);
                // 下载文件
                result = ftp.retrieveFile(fileName, os);
                // 关闭输出流
                os.close();
            }
            // 登出服务器
            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 判断输出流是否存在
                if (null != os) {
                    // 关闭输出流
                    os.close();
                }
                // 判断连接是否存在
                if (ftp.isConnected()) {
                    // 断开连接
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 从FTP服务器删除文件
     *
     * @param url
     *   服务器IP地址
     * @param port
     *   服务器端口
     * @param userName
     *   用户登录名
     * @param password
     *   用户登录密码
     * @param remotePath
     *   服务器文件存储路径
     * @param fileName
     *   服务器文件存储名称
     * @return
     *   <b>true</b>：删除成功
     *   <br/>
     *   <b>false</b>：删除失败
     */
    public static boolean deleteFile (String url, int port, String userName, String password, String remotePath, String fileName) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            // 连接至服务器，端口默认为21时，可直接通过URL连接
            ftp.connect(url ,port);
            // 登录服务器
            ftp.login(userName, password);
            // 判断返回码是否合法
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                // 不合法时断开连接
                ftp.disconnect();
                // 结束程序
                return result;
            }
            // 设置文件操作目录
            ftp.changeWorkingDirectory(remotePath);
            // 设置文件类型，二进制
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 设置缓冲区大小
            ftp.setBufferSize(3072);
            // 设置字符编码
            ftp.setControlEncoding("UTF-8");
            // 获取文件操作目录下所有文件名称
            String[] remoteNames = ftp.listNames();
            // 循环比对文件名称，判断是否含有当前要下载的文件名
            for (String remoteName: remoteNames) {
                if (fileName.equals(remoteName)) {
                    result = true;
                }
            }
            // 文件名称比对成功时，进入删除流程
            if (result) {
                // 删除文件
                result = ftp.deleteFile(fileName);
            }
            // 登出服务器
            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 判断连接是否存在
                if (ftp.isConnected()) {
                    // 断开连接
                    ftp.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }




    public static void main(String[] args) throws FileNotFoundException {

//   try {
//     FileInputStream fis = new FileInputStream(new File("D:\\software\\idea\\ideaIU-2019.2.4.win\\bin\\idea64.exe"));
//     System.out.println(storeFile("139.9.151.109", 22, "root", "Dota2020huaweiyun", "/opt/",  "xx.zip", fis));
//   } catch (FileNotFoundException e) {
//     e.printStackTrace();
//   }
//    
//        File file = new File("C:/Users/freed/Desktop/1.txt");
//        InputStream is = new FileInputStream(file);
//
//        System.out.println(storeFile("127.0.0.1", 21, "feili", "feili", "examples", "2.txt", is));
//        System.out.println(retrieveFile("127.0.0.1", 21, "feili", "feili", "examples/jsp", "index.html", "C:/Users/freed/Desktop"));
//        System.out.println(deleteFile("127.0.0.1", 21, "feili", "feili", "testpath", "1.txt"));

    }


} 