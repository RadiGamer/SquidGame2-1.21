package com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.packet;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.IPacket;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScreenPacket implements IPacket {

    private String packet;

    @Override
    public void read(ByteArrayDataInput buf) throws PacketSerializationException {
        this.packet = buf.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws PacketSerializationException {
        buf.writeUTF(this.packet);
    }

    @Override
    public String getPacketId() {
        return "ScreenPacket";
    }
}
