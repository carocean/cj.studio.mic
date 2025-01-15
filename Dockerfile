# 使用 JDK 1.8.0_421 作为基础镜像
FROM jdk:1.8.0_421

# 拷贝文件到容器
COPY ./cmdtools/gateway/ /app/

# 检查 /app 目录中的文件
RUN ls -lh /app/ || echo "No files in /app/"

# 切换工作目录，准备设置权限
WORKDIR /app

# 设置启动脚本权限
RUN chmod +x gateway.sh || echo "Failed to set execute permission on gateway.sh"

# 输出 JAVA_HOME 和 PATH 环境变量，用于调试
RUN echo "JAVA_HOME is $JAVA_HOME"
RUN echo "PATH is $PATH"

# 启动命令，确保环境变量加载
CMD ["/bin/bash", "gateway.sh","-nohup","&"]