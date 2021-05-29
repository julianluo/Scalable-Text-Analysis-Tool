import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import Model.Message.InvalidInput;
import Model.Message.ValidInput;
import Model.TextLine;
import io.swagger.client.ApiException;
import io.swagger.client.model.ErrMessage;
import io.swagger.client.model.ResultVal;

@WebServlet(name = "Servlet")
public class Servlet extends HttpServlet {

  protected void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    String urlPath = req.getPathInfo();
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");

    if (urlPath == null || urlPath.isEmpty()) {
      ErrMessage errMessage = new ErrMessage();
      errMessage.setMessage("invalid post body");
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(new Gson().toJson(errMessage));
    }

//    String json = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    try {
      Gson gson = new Gson();
      TextLine textLine = gson.fromJson(req.getReader(), TextLine.class);
    }
    catch (Exception e) {
      ErrMessage errMessage = new ErrMessage();
      errMessage.setMessage("invalid post body");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write(new Gson().toJson(errMessage));
    }
    ResultVal resultVal = new ResultVal();
    resultVal.setMessage(1);
    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write(new Gson().toJson(resultVal));
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    String urlPath = req.getPathInfo();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    String jsonString = null;
    int response = 0;
    if (urlPath == null || urlPath.isEmpty()) {
      response = 400;
    } else {
      String json = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
      if (!valTextLine(json)) {
        response = 400;
      } else {
        response = 200;
      }
    }
    if (response == 400) {
      jsonString = gson.toJson(new InvalidInput());
    } else if (response == 200) {
      jsonString = gson.toJson(new ValidInput());
    } else if (response == 201) {
      jsonString = gson.toJson(new ValidInput());
    }
    res.setStatus(response);
    res.setContentType("text/plain");
    PrintWriter out = res.getWriter();
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    out.print(jsonString);
    out.flush();

  }

  protected boolean valTextLine(String json) {
    TextLine convertedObject = new Gson().fromJson(json, TextLine.class);
    return !convertedObject.getText().equals("");
  }

}
