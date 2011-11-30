As of November 2011, Play 2.0 does not support file upload. As Play does not use streams (but Iteratees and Enumerators on top of Netty), this is not trivial to implement. The Play team is expecting this functionality to be done by the RC at the end of January. Until then, this quick hack will work.

It is completely non-idiomatic as it uses the parser Iteratee save the uploaded body to disk (using streams) and then loads it back into commons-fileupload (again using streams). But it works.

    def upload = Action[FileUploadContent](FileUploadContent.parser, { request: Request[FileUploadContent] =>
      request.body.items.foreach { i =>
        if (!i.isFormField) {
          println("Storing file %s".format(i.getName))
          val fout = new FileOutputStream("var/uploads/" + i.getName.replace("..", "-"))
          Streams.copy(i.openStream(), fout, true)
        }
      }
      Ok
    })
    
Note that the signature of the Action method was changed in master Nov 29th, so if you're running off of master, this turns into:

    def upload = Action[FileUploadContent](FileUploadContent.parser) { request: Request[FileUploadContent] =>
      request.body.items.foreach { i =>
        if (!i.isFormField) {
          println("Storing file %s".format(i.getName))
          val fout = new FileOutputStream("var/uploads/" + i.getName.replace("..", "-"))
          Streams.copy(i.openStream(), fout, true)
        }
      }
      Ok
    }

