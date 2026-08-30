import express from "express";
import { request as httpRequest } from "node:http";
import { request as httpsRequest } from "node:https";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = Number(process.env.PORT || 3000);
const apiTarget = new URL(process.env.API_TARGET || "http://localhost:8080");
const webSocketTarget = `${apiTarget.protocol === "https:" ? "wss:" : "ws:"}//${apiTarget.host}`;

app.disable("x-powered-by");
app.use(setSecurityHeaders);
app.use("/api", proxyToSpring);
app.use(express.static(__dirname));

app.get("*", (req, res) => {
  res.sendFile(path.join(__dirname, "index.html"));
});

app.listen(port, () => {
  console.log(`Frontend server: http://localhost:${port}`);
  console.log(`API proxy target: ${apiTarget.origin}`);
});

function setSecurityHeaders(req, res, next) {
  res.setHeader("Content-Security-Policy", [
    "default-src 'self'",
    "script-src 'self'",
    "style-src 'self'",
    "img-src 'self' https: blob:",
    `connect-src 'self' ${apiTarget.origin} ${webSocketTarget}`,
    "font-src 'self'",
    "object-src 'none'",
    "base-uri 'self'",
    "frame-ancestors 'none'",
    `form-action 'self' ${apiTarget.origin}`,
  ].join("; "));
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
  res.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
  next();
}

function proxyToSpring(req, res) {
  const targetPath = req.originalUrl.replace(/^\/api/, "") || "/";
  const requestOptions = {
    protocol: apiTarget.protocol,
    hostname: apiTarget.hostname,
    port: apiTarget.port || (apiTarget.protocol === "https:" ? 443 : 80),
    method: req.method,
    path: `${targetPath}${apiTarget.search}`,
    headers: {
      ...req.headers,
      host: apiTarget.host,
    },
  };

  const proxyRequest = getRequestClient(apiTarget.protocol)(requestOptions, (proxyResponse) => {
    res.status(proxyResponse.statusCode || 500);

    Object.entries(proxyResponse.headers).forEach(([key, value]) => {
      if (value !== undefined) {
        res.setHeader(key, value);
      }
    });

    proxyResponse.pipe(res);
  });

  proxyRequest.on("error", () => {
    res.status(502).json({
      status: 502,
      code: "BAD_GATEWAY",
      message: "백엔드 API 서버에 연결할 수 없습니다.",
    });
  });

  req.pipe(proxyRequest);
}

function getRequestClient(protocol) {
  return protocol === "https:" ? httpsRequest : httpRequest;
}
