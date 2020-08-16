import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    //程序部署
    private void deploy(HttpServletRequest request, HttpServletResponse response) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        List<Future<String>> resultList = new ArrayList<Future<String>>();
        String[] ips = request.getParameterValues("ip");
        String fn = request.getParameter("fn");

        //换行符
         String ls = "<br>";
        //返回到页面的信息
        StringBuffer exctRsl = new StringBuffer(fn).append(ls);

        try{
            for(String ip : ips){
                DeployThread dt = new DeployThread(ip,fn);
                Future<String> res = executor.submit(dt);//异步提交, non blocking.
                resultList.add(res);
            }
            //获取执行结果
            do {
                System.out.println("任务执行中....");
                TimeUnit.SECONDS.sleep(1);
            } while (executor.getCompletedTaskCount() < resultList.size());

            for (int i = 0; i < resultList.size(); i++) {
                Future<String> result = resultList.get(i);
                String rsl = result.isDone()?"部署成功":"部署失败";
                exctRsl.append("节点").append(result.get()).append(rsl).append(ls);
            }
            executor.shutdown();

            request.setAttribute("message", exctRsl);
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
    //部署包上传程序
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
            String host=PropertiesHelper.getValue("host");
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
            //创建ssh通道上传压缩包到服务器
            SSH ssh = new SSH(host, port, username, password);
            ssh.uploadFile(is,targetDir,fileName);
            ssh.closeSession();


            request.setAttribute("message",fileName);
            // 跳转到 代码部署.jsp
            getServletContext().getRequestDispatcher("/deploy.jsp").forward(request, response);
        } catch (Exception ex) {
            request.setAttribute("message",fileName+"上传失败！错误信息: " + ex.getMessage());
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
