package pt.sharecar.messages;

import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle
public interface AppMessages {

    @Message
    String error_invalid_subdomain();

    @Message
    String error_create_schema(String name);

}
