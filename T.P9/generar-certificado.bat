@echo off
REM Genera el certificado autofirmado para el servidor HTTPS (TP9)
REM Ejecutar desde la carpeta T.P9. Requiere keytool (viene con el JDK).
cd /d "%~dp0"
if not exist certificados mkdir certificados
if exist certificados\servidor.p12 del certificados\servidor.p12
if exist certificados\servidor.cer del certificados\servidor.cer
if exist certificados\cliente-truststore.p12 del certificados\cliente-truststore.p12

echo [1/3] Generando par de claves y certificado autofirmado (servidor.p12)...
keytool -genkeypair -alias servidor -keyalg RSA -keysize 2048 -validity 365 -storetype PKCS12 -keystore certificados\servidor.p12 -storepass cambiar123 -dname "CN=localhost, OU=Distribuidos, O=IES 6001, L=Salta, C=AR" -ext "SAN=dns:localhost,ip:127.0.0.1"
if errorlevel 1 goto error

echo [2/3] Exportando el certificado publico (servidor.cer)...
keytool -exportcert -alias servidor -keystore certificados\servidor.p12 -storepass cambiar123 -rfc -file certificados\servidor.cer
if errorlevel 1 goto error

echo [3/3] Creando el truststore del cliente (cliente-truststore.p12)...
keytool -importcert -noprompt -alias servidor -file certificados\servidor.cer -storetype PKCS12 -keystore certificados\cliente-truststore.p12 -storepass cambiar123
if errorlevel 1 goto error

echo Listo. Certificados generados en la carpeta certificados\
goto fin

:error
echo Error al generar los certificados. Verifica que keytool este en el PATH (viene con el JDK).

:fin
