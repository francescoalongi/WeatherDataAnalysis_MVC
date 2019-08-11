package Controller;

import Model.Datum;
import Model.Station;
import Utils.HibernateUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@WebServlet(name = "DownloadDataServlet")
public class DownloadDataServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long beginTimestamp = Long.valueOf(request.getParameter("begin_timestamp")); //1565340900L;
        Long endTimestamp = Long.valueOf(request.getParameter("end_timestamp"));  //1565343600L;
        Integer stationId = Integer.parseInt(request.getParameter("station_id")); //1;
        Map<String, Object> param = new HashMap<>();
        param.put("idStation", stationId);
        Station station = (Station) HibernateUtil.executeSelect(
                "from Station where idStation = :idStation", false, param);
        param.clear();
        param.put("begin_timestamp", beginTimestamp);
        param.put("end_timestamp", endTimestamp);
        String getDataToDownloadQuery = "where timestamp between :begin_timestamp AND :end_timestamp";
        switch (station.getType()) {
            case "city":
                getDataToDownloadQuery = "from DatumCity " + getDataToDownloadQuery;
                break;
            case "country":
                getDataToDownloadQuery = "from DatumCountry " + getDataToDownloadQuery;
                break;
            case "mountain":
                getDataToDownloadQuery = "from DatumMountain " + getDataToDownloadQuery;
                break;
            case "sea":
                getDataToDownloadQuery = "from DatumSea " + getDataToDownloadQuery;
                break;
            default:
                throw new IllegalArgumentException();
        }
        //retrieve the data required
        List data = (List) HibernateUtil.executeSelect(getDataToDownloadQuery, true, param);
        if (data.size() == 0) {
            //@TODO: handle this case: exit and notify the user that the range selected produced zero results
            return;
        }
        //create the file
        PrintWriter printWriter = new PrintWriter("data_" + beginTimestamp + "-" + endTimestamp + ".csv");
        printWriter.println(((Datum) data.get(0)).getFieldsNameAsCSV());
        for (Object datum : data)
            printWriter.println(((Datum) datum).getFieldsAsCSV());
        printWriter.close();
        //download the file
        response.setContentType("APPLICATION/OCTET-STREAM");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "data_" + beginTimestamp + "-" + endTimestamp + ".csv");
        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream("data_" + beginTimestamp + "-" + endTimestamp + ".csv");
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();
    }
}
