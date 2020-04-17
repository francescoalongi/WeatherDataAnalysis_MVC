package Controller;

import Model.*;
import Utils.Neo4jUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.neo4j.driver.Record;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "UploadDataServlet")
@MultipartConfig
public class UploadDataServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String idStation = request.getParameter("idStation");

        Map<String, Object> param = new HashMap<>();
        param.put("idStation", Integer.parseInt(idStation));

        String queryString = "MATCH (n:Station) WHERE id(n) = 0 RETURN {name:n.name, altitude:n.altitude, latitude:n.latitude, longitude:n.longitude, type:n.type}";
        ObjectMapper mapper = new ObjectMapper();
        Record result = (Record) Neo4jUtil.executeSelect(queryString, false, param);
        Station station = mapper.convertValue(result.get(0).asMap(), Station.class);

        Part filePart = request.getPart("newData");
        InputStream fileContent = filePart.getInputStream();
        Reader in = new InputStreamReader(fileContent, StandardCharsets.UTF_8);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

        String tempFilePath = Neo4jUtil.neo4jPath + "/import/temp.csv";
        FileWriter writer = new FileWriter(tempFilePath);
        CSVPrinter csvPrinter = CSVFormat.EXCEL.withFirstRecordAsHeader().print(writer);
        csvPrinter.printRecords(records);
        csvPrinter.close();

        queryString = "USING PERIODIC COMMIT 500 LOAD CSV WITH HEADERS FROM 'file:///temp.csv' AS row " +
                "MATCH (s:Station) WHERE id(s) = $idStation CREATE (n:Datum{datetime:datetime({epochSeconds: toInteger(row.timestamp)}), temperature:toFloat(row.temperature), " +
                "humidity:toFloat(row.humidity), windModule:toFloat(row.windModule), windDirection:row.windDirection, " +
                "pressure: toFloat(row.pressure), rain:toFloat(row.rain), ";
        switch (station.getType().toLowerCase()) {
            case "city":
                queryString += "pollutionLevel:toFloat(row.pollutionLevel)";
                break;
            case "country":
                queryString += "dewPoint:toFloat(row.dewPoint)";
                break;
            case "mountain":
                queryString += "snowLevel:toFloat(row.snowLevel)";
                break;
            case "sea":
                queryString += "uvRadiation:toFloat(row.uvRadiation)";
                break;
            default:
                throw new IllegalArgumentException();
        }


        queryString += "}), (s)-[:HAS_ACQUIRED]->(n)";
        long bTime = System.currentTimeMillis();
        Neo4jUtil.executeInsert(queryString, param, true);
        long eTime = System.currentTimeMillis();
        System.out.println(queryString + " --> " + (eTime - bTime));
        File file = new File(tempFilePath);
        file.delete();

        request.setAttribute("outcomeUpload", "Your .csv file has been successfully uploaded.");
        request.getRequestDispatcher("/LoadStations").forward(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("Error", "Error! GET request not supported");
        getServletContext().getRequestDispatcher("/Error").forward(request, response);
    }
}
