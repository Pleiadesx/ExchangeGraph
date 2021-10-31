const createProxyMiddleWare = require('http-proxy-middleware').createProxyMiddleware


module.exports = function (app) {
    console.log("configuring app")

    app.use(
        '/rsocket',
        createProxyMiddleWare({
            target: `ws://localhost:${process.env.PORT || 8080}/rsocket`,
            changeOrigin: true,
            ws: true
        })
    )


}
