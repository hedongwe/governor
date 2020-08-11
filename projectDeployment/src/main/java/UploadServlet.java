import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.util.List;

public class UploadServlet extends HttpServlet {
    // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "upload";
    String fileName = "";
    // 上传配置-单位字节
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 20 * 2 ;  // 40MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 100 * 2; // 200MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 100 * 2; // 200MB

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestUrl = request.getRequestURL().toString();
        if(requestUrl.indexOf("deploy.uf") > -1 ){
            deploy(request,response);
        }else{
            upload(request,response);
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    private void deploy(HttpServletRequest request, HttpServletResponse response) {

        String[] ips = request.getParameterValues("ip");
        String fn = request.getParameter("fn");
        try{
            for(String ip : ips){
                DeployThread dt = new DeployThread(ip,fn);
                Thread th = new Thread(dt);
                th.setName(ip);
                th.start();
                System.out.println(th.getName());
            }

            request.setAttribute("message",
                    fn);
            // 跳转到 代码部署.jsp
            getServletContext().getRequestDispatcher("/successPage.jsp").forward(request, response);
        }catch (Exception ex){
            request.setAttribute("message",
                    fn+"上传失败！错误信息: " + ex.getMessage());
            ex.printStackTrace();
            try {
                getServletContext().getRequestDispatcher("/errPage.jsp").forward(request, response);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void upload(HttpServletRequest request, HttpServletResponse response) {
            //2.开始配置上传参数-创建fileItem工厂
            DiskFileItemFactory factory = new DiskFileItemFactory();
            // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
            factory.setSizeThreshold(MEMORY_THRESHOLD);
            // 设置临时存储目录
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
            //创建文件上传核心组件
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 设置最大上传文件的阈值
            upload.setFileSizeMax(MAX_FILE_SIZE);
            // 设置最大请求值 (包含文件和表单数据)
            upload.setSizeMax(MAX_REQUEST_SIZE);
            // 中文处理
            upload.setHeaderEncoding("UTF-8");

            // 构造临时路径来存储上传的文件
            // 这个路径相对当前应用的目录

        try {
            //远程服务器登陆准备
            String servers = PropertiesHelper.getValue("servers");
            String[] serverArr = servers.split(",");

            String username=PropertiesHelper.getValue("username");
            String password=PropertiesHelper.getValue("password");
            int port = Integer.valueOf(PropertiesHelper.getValue("port"));
            String targetDir=PropertiesHelper.getValue("targetDir");


            //部署文件准备
            InputStream is = null;
            // 解析请求的内容提取文件数据
            List<FileItem> formItems = upload.parseRequest(request);
            if (formItems != null && formItems.size() > 0) {
                // 迭代表单数据
                for (FileItem item : formItems) {
                    // 处理不在表单中的字段
                    if (!item.isFormField()) {
                        fileName = new File(item.getName()).getName();
                        is = item.getInputStream();
                    }
                }
            }

            for(String ip : serverArr){
                SSH ssh = new SSH(ip, port, username, password);
                ssh.uploadFile(is,targetDir,fileName);
//                String command = "sh "+PropertiesHelper.getValue("tcommand");
//                if(fileName.indexOf(".war.") > -1){
//                    command = "sh "+PropertiesHelper.getValue("jcommand");
//                }
//                System.out.println(command+" "+fileName.substring(0,fileName.indexOf(".")));
//                ssh.execShell(command+" "+fileName.substring(0,fileName.indexOf(".")));//执行远程shell脚本
                ssh.closeSession();
            }


            request.setAttribute("message",
                    fileName);
            // 跳转到 代码部署.jsp
            getServletContext().getRequestDispatcher("/deploy.jsp").forward(request, response);
        } catch (Exception ex) {
            request.setAttribute("message",
                    fileName+"上传失败！错误信息: " + ex.getMessage());
            ex.printStackTrace();
            try {
                getServletContext().getRequestDispatcher("/errPage.jsp").forward(request, response);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
