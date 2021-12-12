package com.ssk.binlog.event.parser;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.ssk.binlog.BinlogException;
import com.ssk.binlog.event.EventEntity;
import com.ssk.binlog.event.EventEntityType;
import com.ssk.binlog.event.parser.converter.CommonConverterProcessor;
import com.ssk.binlog.tablemeta.TableMetaEntity;
import com.ssk.binlog.tablemeta.TableMetaFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateEventParser implements IEventParser {

    CommonConverterProcessor commonConverterProcessor = new CommonConverterProcessor();

    TableMetaFactory tableMetaFactory;

    UpdateEventParser(TableMetaFactory tableMetaFactory) {
        this.tableMetaFactory = tableMetaFactory;
    }

    @Override
    public List<EventEntity> parse(Event event) throws BinlogException {
        List<EventEntity> eventEntityList = new ArrayList<>();
        UpdateRowsEventData updateRowsEventData = event.getData();
        TableMetaEntity tableMetaEntity = tableMetaFactory.getTableMetaEntity(updateRowsEventData.getTableId());
        List<Map.Entry<Serializable[], Serializable[]>> rows = updateRowsEventData.getRows();
        rows.forEach(rowMap -> {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setEvent(event);
            eventEntity.setDatabaseName(tableMetaEntity.getDbName());
            eventEntity.setTableName(tableMetaEntity.getTableName());
            eventEntity.setEventEntityType(EventEntityType.UPDATE);
            List<TableMetaEntity.ColumnMetaData> columnMetaDataList = tableMetaEntity.getColumnMetaDataList();
            //解析update前后的数据
            String[] before = commonConverterProcessor.convertToString(rowMap.getKey(), columnMetaDataList);
            String[] after = commonConverterProcessor.convertToString(rowMap.getValue(), columnMetaDataList);

            List<String> columns = new ArrayList<>();
            List<Object> changeBefore = new ArrayList<>();
            List<Object> changeAfter = new ArrayList<>();
            for (int i = 0; i < before.length; i++) {
                columns.add(columnMetaDataList.get(i).getName());
                changeBefore.add(before[i]);
                changeAfter.add(after[i]);
            }
            eventEntity.setColumns(columnMetaDataList);
            eventEntity.setChangeBefore(changeBefore);
            eventEntity.setChangeAfter(changeAfter);

            eventEntityList.add(eventEntity);
        });
        return eventEntityList;
    }
}
