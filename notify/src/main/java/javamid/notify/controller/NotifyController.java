package javamid.notify.controller;

import javamid.notify.service.NotifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotifyController {

  private final NotifyService notifyService;
  public NotifyController( NotifyService notifyService ){ this.notifyService = notifyService; }

  @GetMapping("/{userId}")
  public ResponseEntity<?> getNotify( @PathVariable Long  userId,
                                      @RequestParam(required = false) List<Long> noteId
                                    ) {


    String mockString = """
    [
      {
        "id": 1111,
        "title": "Новое сообщение",
        "message": "Вам пришло новое сообщение в чате",
        "createdAt": "2023-10-25T14:30:00",
        "read": false,
        "type": "INFO"
      },
      {
        "id": 2222,
        "title": "Пополнение счета",
        "message": "Ваш счет пополнен на 1000 RUB",
        "createdAt": "2023-10-25T14:25:00",
        "read": false,
        "type": "SUCCESS"
      }
    ]
    """;


    System.out.println( "checked: " + noteId );

    return ResponseEntity.ok().body( mockString );



  }
}