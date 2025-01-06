package com.radi.networking;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.IPacket;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class CompletionPacket implements IPacket {
    private String message;

    // Default constructor for deserialization
    public CompletionPacket() {}

    // Constructor to initialize the message
    public CompletionPacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws PacketSerializationException {
        try {
            this.message = buf.readUTF();
        } catch (Exception e) {
            throw new PacketSerializationException("Failed to read CompletionPacket", e);
        }
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws PacketSerializationException {
        try {
            buf.writeUTF(this.message);
        } catch (Exception e) {
            throw new PacketSerializationException("Failed to write CompletionPacket", e);
        }
    }

    @Override
    public String getPacketId() {
        return "CompletionPacket";
    }
}
