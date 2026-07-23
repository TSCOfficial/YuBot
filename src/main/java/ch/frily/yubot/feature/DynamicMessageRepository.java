package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DynamicMessageRepository {

    /**
     * Raw reference to a dynamic message.
     * <br>
     * This is used to be able to send a new dynamic message to a channel, where the specified message was deleted or is unreachable.
     * @param name registry name of the dynamic message
     * @param channelId current target channel id
     * @param messageId current target message id
     */
    public record DynamicMessageReference(String name, long channelId, long messageId) {
    }

    /**
     * Get a dynamic message reference by its registry name.
     * @param name
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static DynamicMessageReference getDynamicMessageReference(String name) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.DYNAMIC_MESSAGE);
        query.select().where(Table.DynamicMessageColumn.NAME, DatabaseQuery.Operator.EQUALS, name);

        ResultSet resultSet = query.executeDataQuery();

        if (!resultSet.next()) {
            throw new NullPointerException("Dynamic message " + name + " not found.");
        }

        long channelId = resultSet.getLong(Table.DynamicMessageColumn.CHANNEL_ID.getColumn());
        long messageId = resultSet.getLong(Table.DynamicMessageColumn.MESSAGE_ID.getColumn());

        return new DynamicMessageReference(name, channelId, messageId);
    }

    /**
     * retrieve a dynamic message by its registry name
     * @param name
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static CompletableFuture<DynamicMessage> getDynamicMessage(String name) throws SQLException, ClassNotFoundException {
        DynamicMessageReference reference = getDynamicMessageReference(name);
        return DynamicMessage.retrieve(reference.name(), reference.channelId(), reference.messageId());
    }

    public static boolean exists(String name) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.DYNAMIC_MESSAGE);
        query.select(Table.DynamicMessageColumn.NAME)
                .where(Table.DynamicMessageColumn.NAME, DatabaseQuery.Operator.EQUALS, name);

        ResultSet resultSet = query.executeDataQuery();
        return resultSet.next();
    }

    public static void createDynamicMessage(DynamicMessage dynamicMessage) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.DYNAMIC_MESSAGE);
        query.insert(Table.DynamicMessageColumn.NAME, dynamicMessage.name());
        query.insert(Table.DynamicMessageColumn.CHANNEL_ID, dynamicMessage.message().getChannel().getIdLong());
        query.insert(Table.DynamicMessageColumn.MESSAGE_ID, dynamicMessage.message().getIdLong());

        query.executeQuery();
    }

    public static void deleteDynamicMessage(DynamicMessage dynamicMessage) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.DYNAMIC_MESSAGE);
        query.where(Table.DynamicMessageColumn.NAME, DatabaseQuery.Operator.EQUALS, dynamicMessage.name()).delete();
        query.executeQuery();
    }

    public static void updateDynamicMessage(DynamicMessage dynamicMessage) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.DYNAMIC_MESSAGE);
        query.update(Table.DynamicMessageColumn.CHANNEL_ID, dynamicMessage.message().getChannel().getIdLong());
        query.update(Table.DynamicMessageColumn.MESSAGE_ID, dynamicMessage.message().getIdLong());
        query.where(Table.DynamicMessageColumn.NAME, DatabaseQuery.Operator.EQUALS, dynamicMessage.name());

        query.executeQuery();
    }

    public static void upsertDynamicMessage(DynamicMessage dynamicMessage) throws SQLException, ClassNotFoundException {
        if (exists(dynamicMessage.name())) {
            log.info("Dynamic message {} already exists, updating...", dynamicMessage.name());
            updateDynamicMessage(dynamicMessage);
        } else {
            log.info("Dynamic message {} does not exist, creating...", dynamicMessage.name());
            createDynamicMessage(dynamicMessage);
        }
    }
}
