echo 'yes' | keytool -keystore truststore_with_globalsign.jks -importcert -alias globalsign -storepass changeit -file "Builtin Object Token_GlobalSign Root CA - R3.pem"
echo 'yes' | keytool -keystore truststore_with_eszigno2017.jks -importcert -alias eszigno2017 -storepass changeit -file "e-Szigno Root CA 2017.pem"
