package Controller;

import Model.MinimizedStation;
import Utils.Neo4jUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Record;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "LoadStationsServlet")
public class LoadStationsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<Record> results = (List<Record>) Neo4jUtil.executeSelect("MATCH (S:Station) RETURN {id:id(S), name:S.name, type:S.type}", true);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<MinimizedStation> list = new ArrayList<>();
        for (Record record : results) {
            list.add(mapper.convertValue(record.get(0).asMap(), MinimizedStation.class));
        }
        String json = mapper.writeValueAsString(list);

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } else {
            // this branch is taken only if the request is a non-ajax request, this happens only when we need to reload the Homepage
            request.setAttribute("stations", json);
            //when the QueryDataGraphServlet will be ready, this servlet must forward to that servlet
            request.getRequestDispatcher("/Homepage").forward(request, response);
        }
    }
}