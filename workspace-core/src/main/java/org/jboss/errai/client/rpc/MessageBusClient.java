package org.jboss.errai.client.rpc;

import org.jboss.errai.client.framework.AcceptsCallback;
import org.jboss.errai.client.framework.MessageCallback;
import org.jboss.errai.client.rpc.protocols.ClientBusServer;

import java.util.Map;
import java.util.Set;


public class MessageBusClient {
    private static ClientBusServer bus;

    public static ClientBusServer getBus() {
        return bus;
    }

    public static void setBus(ClientBusServer bus) {
        MessageBusClient.bus = bus;
    }

    public static void unsubscribeAll(String subject) {
        bus.unsubscribeAll(subject);
    }

    public static void subscribe(String subject, MessageCallback callback, Object subscriberData) {
        bus.subscribe(subject, callback, subscriberData);
    }

    public static void subscribe(String subject, MessageCallback callback) {
        bus.subscribe(subject, callback);
    }

    public static void subscribeOnce(String subject, MessageCallback callback, Object subscriberData) {
        bus.subscribeOnce(subject, callback, subscriberData);
    }

    public static void subscribeOnce(String subject, MessageCallback callback) {
        bus.subscribeOnce(subject, callback);
    }
    
    public static void conversationWith(final CommandMessage message, final MessageCallback callback) {
        bus.conversationWith(message, callback);
    }

    public static boolean isSubscribed(String subject) {
        return bus.isSubscribed(subject);
    }

    public static Map<String, Set<Object>> getCapturedRegistrations() {
        return bus.getCapturedRegistrations();
    }

    public static void beginCapture() {
        bus.beginCapture();
    }

    public static void endCapture() {
        bus.endCapture();
    }

    public static void unregisterAll(Map<String, Set<Object>> all) {
        bus.unregisterAll(all);
    }

    public static void store(String subject, Map<String, Object> message) {
        bus.store(subject, message);
    }

    public static void store(String subject, CommandMessage message) {
        bus.store(subject, message);
    }

    public static void store(String subject, Enum commandType) {
        bus.store(subject, commandType);
    }

    public static void store(CommandMessage message) {
        bus.store(message);
    }

    public static void addOnSubscribeHook(AcceptsCallback callback) {
        bus.addOnSubscribeHook(callback);
    }

    public static void addOnUnsubscribeHook(AcceptsCallback callback) {
        bus.addOnUnsubscribeHook(callback);
    }

    public static Set<String> getAllLocalSubscriptions() {
        return bus.getAllLocalSubscriptions();
    }
}

