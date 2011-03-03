package fr.xebia.usiquizz.core.persistence.serialization;

import com.esotericsoftware.kryo.Kryo;
import fr.xebia.usiquizz.core.persistence.User;

import java.nio.ByteBuffer;

public class UserSerializer {

    private Kryo kryo = new Kryo();

    public UserSerializer() {
        kryo.register(User.class);
    }

    public byte[] serializeUser(User user) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(128);
        kryo.writeObjectData(buffer, user);
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    public User deserializeUser(byte[] serializedUser) {
        return kryo.readObjectData(ByteBuffer.wrap(serializedUser), User.class);
    }
}
