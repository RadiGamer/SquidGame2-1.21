package com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network;

import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

/**
 * Interfaz que define los métodos para la serialización y deserialización de paquetes.
 * Las clases que implementen esta interfaz deben proporcionar la lógica para leer y escribir
 * datos de/a un buffer de bytes.
 */
public interface IPacket {
    /**
     * Lee los datos del paquete desde un buffer de entrada.
     *
     * @param buf El buffer de entrada del cual se leerán los datos.
     * @throws PacketSerializationException Si ocurre un error durante la lectura o deserialización del paquete.
     */
    void read(final ByteArrayDataInput buf) throws PacketSerializationException;

    /**
     * Escribe los datos del paquete en un buffer de salida.
     *
     * @param buf El buffer de salida en el cual se escribirán los datos.
     * @throws PacketSerializationException Si ocurre un error durante la escritura o serialización del paquete.
     */
    void write(final ByteArrayDataOutput buf) throws PacketSerializationException;

    /**
     * Identificador interno del paquete.
     * Utilizado para Loggers
     *
     * @return String
     */
    String getPacketId();
}