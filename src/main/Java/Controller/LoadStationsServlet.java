package Controller;

import Model.MinimizedStation;
import Model.Station;
import Utils.HibernateUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LoadStationsServlet")
public class LoadStationsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<MinimizedStation> results = (List<MinimizedStation>) HibernateUtil.executeSelect("SELECT new Model.MinimizedStation(idStation,name,type) FROM Station", true);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(results);

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