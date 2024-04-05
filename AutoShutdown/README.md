1. 生成my.keystore
```shell
# lineage os，平台签名文件路径。取决于平台
cd build/make/target/product/security
openssl pkcs8 -in platform.pk8 -inform DER -outform PEM -out shared.priv.pem -nocrypt
openssl pkcs12 -export -in platform.x509.pem -inkey shared.priv.pem -out platform.pk12 -name platform
keytool -importkeystore -deststorepass 123456 -destkeypass 123456 -destkeystore debug.keystore -srckeystore platform.pk12 -srcstoretype PKCS12 -srcstorepass 123456 -alias platform
```
2. 