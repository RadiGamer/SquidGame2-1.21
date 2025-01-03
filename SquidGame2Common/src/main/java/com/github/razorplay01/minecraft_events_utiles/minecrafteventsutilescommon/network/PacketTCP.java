package com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network;

import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketInstantiationException;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketNotFoundException;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketRegistrationException;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.packet.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase que maneja la serialización y deserialización de paquetes TCP.
 * Utiliza un registro bidireccional para mapear IDs de paquetes a clases de paquetes.
 */
public class PacketTCP {
    public static final String PACKET_CHANNEL = "squidgame2:open_screen";

    private static final BiMap<Integer, Class<? extends IPacket>> packetRegistry = HashBiMap.create();
    private static final AtomicInteger nextId = new AtomicInteger(0);
    private static PacketTCP instance;

    // Inicialización estática del registro de paquetes
    static {
        registerPacket(0, EmptyPacket.class);
        registerPacket(1, ScreenPacket.class);

    }

    private PacketTCP() {
    }

    public static synchronized PacketTCP getInstance() {
        if (instance == null) {
            instance = new PacketTCP();
        }
        return instance;
    }

    /**
     * Registra un nuevo tipo de paquete con su ID correspondiente.
     *
     * @param id          El ID único del paquete.
     * @param packetClass La clase del paquete a registrar.
     * @throws PacketRegistrationException Si el ID ya está registrado.
     */
    public static void registerPacket(int id, Class<? extends IPacket> packetClass) {
        if (packetRegistry.containsKey(id)) {
            throw new PacketRegistrationException("Packet ID " + id + " is already registered.");
        }
        packetRegistry.put(id, packetClass);
    }

    /**
     * Registra un nuevo tipo de paquete asignándole automáticamente un ID único.
     *
     * @param packetClass La clase del paquete a registrar.
     * @throws PacketRegistrationException Si la clase del paquete ya está registrada.
     */
    public static void registerPacket(Class<? extends IPacket> packetClass) {
        if (packetRegistry.containsValue(packetClass)) {
            throw new PacketRegistrationException("Packet class " + packetClass.getName() + " is already registered.");
        }

        int id;
        do {
            id = nextId.getAndIncrement();
        } while (packetRegistry.containsKey(id));

        packetRegistry.put(id, packetClass);
    }

    /**
     * Obtiene el tipo de paquete (ID) para una instancia de paquete dada.
     *
     * @param packet La instancia del paquete.
     * @return El ID del tipo de paquete.
     * @throws PacketNotFoundException Si la clase del paquete no está registrada.
     */
    private static int getPacketType(IPacket packet) {
        return Optional.ofNullable(packetRegistry.inverse().get(packet.getClass()))
                .orElseThrow(() -> new PacketNotFoundException("Packet class not registered: " + packet.getClass().getName()));
    }

    /**
     * Serializa un paquete a un array de bytes.
     *
     * @param packet El paquete a serializar.
     * @return Un array de bytes que representa el paquete serializado.
     * @throws PacketSerializationException Si ocurre un error durante la escritura.
     */
    public static byte[] write(IPacket packet) throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        int packetType = getPacketType(packet);
        out.writeByte(packetType);
        packet.write(out);
        return out.toByteArray();
    }

    /**
     * Deserializa un paquete a partir de un ByteArrayDataInput.
     *
     * @param buf El ByteArrayDataInput que contiene los datos del paquete.
     * @return Una instancia de IPacket deserializada.
     * @throws PacketInstantiationException Si ocurre un error durante la instanciación del paquete.
     * @throws PacketSerializationException Si ocurre un error durante la lectura.
     */
    public static IPacket read(ByteArrayDataInput buf) throws PacketInstantiationException, PacketSerializationException {
        int packetType = buf.readByte();
        Class<? extends IPacket> packetClass = packetRegistry.get(packetType);

        if (packetClass == null) {
            throw new PacketInstantiationException("Could not find packet with ID " + packetType, null);
        }

        try {
            IPacket packet = packetClass.getDeclaredConstructor().newInstance();
            packet.read(buf);
            return packet;
        } catch (ReflectiveOperationException e) {
            throw new PacketInstantiationException("Error instantiating packet with ID " + packetType, e);
        }
    }

    /**
     * Obten el id de un paquete registrado.
     *
     * @param packetClass IPacket ha obtener su id.
     * @return Una instancia de IPacket deserializada.
     * @throws PacketNotFoundException Si la clase del paquete no está registrada.
     */
    public static int getPacketId(Class<? extends IPacket> packetClass) {
        Integer id = packetRegistry.inverse().get(packetClass);
        if (id == null) {
            throw new PacketNotFoundException("Packet class " + packetClass.getName() + " is not registered.");
        }
        return id;
    }

    /**
     * Limpia el registro de paquetes. Útil para tests.
     */
    public static void clearRegistry() {
        packetRegistry.clear();
    }
}