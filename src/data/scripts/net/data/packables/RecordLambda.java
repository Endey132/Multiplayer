package data.scripts.net.data.packables;

import data.scripts.net.data.records.DataRecord;

public class RecordLambda<T> {

    public final DataRecord<T> record;
    public final SourceExecute<T> sourceExecute;
    public final DestExecute<T> destExecute;

    public RecordLambda(DataRecord<T> record, SourceExecute<T> sourceExecute, DestExecute<T> destExecute) {
        this.record = record;
        this.sourceExecute = sourceExecute;
        this.destExecute = destExecute;
    }

    public boolean sourceExecute() {
        return record.sourceExecute(sourceExecute);
    }

    public void overwrite(int tick, Object delta) {
        record.overwrite(delta);
    }

    public void destExecute(EntityData packable) {
        destExecute.execute(record.getValue(), packable);
    }
}
