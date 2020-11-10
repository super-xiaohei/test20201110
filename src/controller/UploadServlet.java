package controller;

import com.sun.deploy.net.URLEncoder;
import domain.Users;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import service.UserService;
import service.impl.UserServiceImpl;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@WebServlet(urlPatterns = "/upload")
public class UploadServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1、创建DiskFileItemFactory对象，设置缓冲区大小和临时文件目录。
        DiskFileItemFactory dis = new DiskFileItemFactory();
        dis.setSizeThreshold(1024*1024*60);
        File file = new File("d://temp");
        dis.setRepository(file);
        //2、使用DiskFileItemFactory 对象创建ServletFileUpload对象，并设置上传文件的大小限制。
        ServletFileUpload servletFileUpload = new ServletFileUpload(dis);
        //3、调用ServletFileUpload.parseRequest方法解析request对象，得到一个保存了所有上传内容的List对象。
        try {
            List<FileItem> fileItems = servletFileUpload.parseRequest(req);
            //4、对list进行迭代，每迭代一个FileItem对象，调用其isFormField方法判断是否是上传文件：
            //4.1、 为普通表单字段，则调用getFieldName、getString方法得到字段名和字段值。
            //4.2、为上传文件，则调用getInputStream方法得到数据输入流，从而读取上传数据。
            for (FileItem fileItem : fileItems) {
                if (!fileItem.isFormField()) {
                    System.out.println("是文件");
                    String realPath = req.getServletContext().getRealPath("static/imgs");
                    //System.out.println(realPath);//C:\Users\Administrator\IdeaProjects\suncaper2\out\artifacts\proDemo_war_exploded\static\imgs
                    String name = fileItem.getName();
                    //name = java.net.URLEncoder.encode(name,"utf-8");
                    name = new String(name.getBytes("gbk"),"iso8859-1");
                    //获取客户端信息
                    /*String ua = req.getHeader("User-Agent");
                    //判断客户端是否为火狐
                    if(ua.contains("Firefox")){
                        //若为火狐使用BASE64编码
                        name = "=?utf-8?B?"+new BASE64Encoder()
                                .encode(name.getBytes("utf-8"))+"?=";
                    }else {
                        //否则使用UTF-8
                        name = URLEncoder.encode(name, "utf-8");
                    }*/
                    File file1 = new File(realPath + "/" + name);
                    //上传是把文件放到输出流里面，得到文件的输入流
                    FileOutputStream fileOutputStream = new FileOutputStream(file1);
                    InputStream inputStream = fileItem.getInputStream();
                    byte[] bs = new byte[1024*1024];
                    int len;
                    while (true) {
                        len = inputStream.read(bs);
                        if(len == -1){
                            break;
                        }else {
                            fileOutputStream.write(bs,0,len);
                        }
                    }
                    fileOutputStream.flush();
                    //将路径写进数据库
                    HttpSession session = req.getSession();
                    Users users = (Users)session.getAttribute("loginUser");
                    users.setUrl("/static/imgs/" + name);
                    userService.updateImg(users);
                    session.setAttribute("loginUser",users);
                    req.getRequestDispatcher("/user/user-imgHead.jsp").forward(req,resp);
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }

    }
}
