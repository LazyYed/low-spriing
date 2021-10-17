package com.lazy.spring.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import com.lazy.spring.annotation.*;
import com.lazy.utils.CommonUtil;

/**
 * @author lazyyedi@gamil.com
 * @creed: 我不能创造的东西，我就无法理解
 * @date 2021/10/16 下午6:45
 */
public class DispatcherServlet extends HttpServlet {
    //保存扫描到的className;
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> ioc = new HashMap(64);
    private Map<String, Method> handleMapping = new HashMap(64);

    @Override
    public void init(ServletConfig config) throws ServletException {
        //获取配置文件
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        //解析文件获取到扫描包路径
        parserProperties(contextConfigLocation);
        doIOC();
        doDI();
        //url与方法进行映射
        doHandlerMapping();
    }

    private void doHandlerMapping() {
        if (this.ioc.isEmpty()) return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            String url = null;
            //判断类上师傅又requestMapping注解
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                url = requestMapping.value().trim();
                ;
            }
            //判断方法上是否有Mapping注解
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    url = ("/" + url + "/" + requestMapping.value()).replaceAll("/+", "/");
                    this.handleMapping.put(url, method);
                }
            }
        }

        System.out.println();
    }

    //依赖注入
    private void doDI() {
        if (this.ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            for (Field field : value.getClass().getDeclaredFields()) {
                System.out.println(field);
                //判断是否包含@Autowired
                if (field.isAnnotationPresent(Autowired.class)) {
                    String beanName = CommonUtil.lowerFirst(field.getType().getSimpleName());
                    field.setAccessible(true);
                    //从容器中获取Bean
                    Object instance = ioc.get(beanName);
                    try {
                        field.set(value, instance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //控制反转
    private void doIOC() {
        if (this.classNames.isEmpty()) {
            return;
        }
        //迭代classNames集合，通过反射Class.forName()  获取到每个.class文件的Class对象
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                String beanName;
                //判断Class上是否有注解: @Controller   @Service   @Repository  @Component
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Controller controller = clazz.getAnnotation(Controller.class);
                    //获取controller中的属性
                    if (!controller.value().equals("")) {
                        beanName = controller.value();
                    }
                    beanName = CommonUtil.lowerFirst(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service controller = clazz.getAnnotation(Service.class);
                    //获取controller中的属性
                    if (!controller.value().equals("")) {
                        beanName = controller.value();
                    }
                    beanName = CommonUtil.lowerFirst(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //判断service可能会是接口
                    for (Class<?> interfaceClazz : clazz.getInterfaces()) {
                        ioc.put(CommonUtil.lowerFirst(interfaceClazz.getSimpleName()), instance);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parserProperties(String contextConfigLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            String scanPackage = properties.getProperty("scanPackage");
            URL url = this.getClass().getClassLoader()
                    .getResource(scanPackage.replace(".", File.separator));
            System.out.println(url);
            File file = new File(url.getFile());
            recursionFile(file, scanPackage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recursionFile(File file, String scanPackage) {
        for (File listFile : file.listFiles()) {
            if (listFile.isDirectory()) {
                //如果是文件就继续执行
                recursionFile(listFile, scanPackage + "." + listFile.getName());
            } else {
                if (listFile.getName().endsWith(".class")) {
                    //拼接class路径
                    String className = scanPackage + "." + listFile.getName().replace(".class", "");
                    classNames.add(className);
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //获取请求路径
        String url = req.getRequestURI();
        System.out.println(url);
        //获取到URL后 根据url找到对应的方法
        if (!this.handleMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found");
            return;
        }
        Method method = this.handleMapping.get(url);
        Object o = ioc.get(CommonUtil.lowerFirst(method.getDeclaringClass().getSimpleName()));
        Object[] params = getMethodParams(req, resp, method, o);
        try {
            method.invoke(o, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object[] getMethodParams(HttpServletRequest req, HttpServletResponse resp, Method method, Object o) {
        //遍历参数列表，记录下标位置
        Object[] params = new Object[method.getParameterTypes().length];
        Map<String, Integer> paramIndexMapping = new HashMap<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation parameterAnnotation : parameterAnnotations[i]) {
                System.out.println(parameterAnnotation);
                if (parameterAnnotation instanceof RequestParam) {
                    String value = ((RequestParam) parameterAnnotation).value();
                    paramIndexMapping.put(value,i);
                }
            }
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == HttpServletRequest.class) {
                params[i] = req;
            } else if (parameterTypes[i] == HttpServletResponse.class) {
                params[i] = resp;
            }
        }

        //上面条件处理完后 就是普通的参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        for (Map.Entry<String, String[]> paramEntry : parameterMap.entrySet()) {
            String paramKey = paramEntry.getKey();
            String paramValue = Arrays.toString(paramEntry.getValue()).replaceAll("\\[|\\]", "");
            System.out.println("key:" + paramKey);
            System.out.println("value:" + paramValue);
            if (!paramIndexMapping.containsKey(paramKey)) {
                continue;
            }
            Integer index = paramIndexMapping.get(paramKey);
            params[index] = CommonUtil.string2Other(paramValue, parameterTypes[index]);
        }
        return params;
    }
}
