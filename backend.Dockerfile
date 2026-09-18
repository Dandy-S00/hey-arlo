FROM node:20-alpine
WORKDIR /app
COPY server/index.mjs ./index.mjs
ENV NODE_ENV=production PORT=3001
EXPOSE 3001
USER node
CMD ["node", "index.mjs"]
