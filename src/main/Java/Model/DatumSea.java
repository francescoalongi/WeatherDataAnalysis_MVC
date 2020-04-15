package Model;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl=DatumSea.class)
public class DatumSea extends Datum {

    private Float uvRadiation;

    public DatumSea() {}

    public DatumSea(//DatumPK datumPK,
                    Long timestamp, String idStation, Float temperature, Float pressure, Float humidity, Float rain, Float windModule, String windDirection, Float uvRadiation) {
        super(timestamp,idStation,temperature,pressure,humidity,rain,windModule,windDirection);
        this.uvRadiation = uvRadiation;
    }

    public void setUvRadiation(Float uvRadiation) {
        this.uvRadiation = uvRadiation;
    }

    public Float getUvRadiation() {
        return uvRadiation;
    }

    @Override
    public String getFieldsNameAsCSV() {
        return super.getFieldsNameAsCSV() + "," + "uvRadiation";
    }

    @Override
    public String getFieldsAsCSV() {
        return super.getFieldsAsCSV() + "," + uvRadiation;
    }

}
