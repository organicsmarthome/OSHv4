package osh.datatypes.logger;

import java.util.UUID;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class JPAUUIDConverter implements Converter {

	private static final long serialVersionUID = 6982191183318975736L;

	@Override
    public Object convertObjectValueToDataValue(Object objectValue,
            Session session) {
		if( objectValue == null )
			return "null";
        return objectValue.toString();
    }

    @Override
    public UUID convertDataValueToObjectValue(Object dataValue,
            Session session) {
        return UUID.fromString((String) dataValue);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
        final DatabaseField field;
        if (mapping instanceof DirectCollectionMapping) {
            // handle @ElementCollection...
            field = ((DirectCollectionMapping) mapping).getDirectField();
        } else {
            field = mapping.getField();
        }

        field.setSqlType(java.sql.Types.VARCHAR);
        field.setLength(40);
        //field.setTypeName("uuid");
        //field.setColumnDefinition("UUID");
    }
}