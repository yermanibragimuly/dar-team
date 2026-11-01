FROM nginx:alpine

COPY ./dist /usr/share/nginx/html

RUN printf "server { \
  listen 80; \
  server_name _; \
  root /usr/share/nginx/html; \
  index index.html; \
  location / { try_files \$uri /index.html; } \
}\n" > /etc/nginx/conf.d/default.conf
