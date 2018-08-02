package soap;

import javax.jws.WebMethod;
import javax.jws.WebService;

    @WebService
    public class PublicacionWebService {

        @WebMethod
        public String holaMundo(String hola){
            return hola;
        }

    }
