package javamid.notify.controller;

import javamid.notify.model.Notification;
import javamid.notify.model.TransferDto;
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



    System.out.println( "checked: " + noteId );

    if( noteId != null) {
      for( Long id: noteId )  notifyService.markAsRead(id);
    }

    List<Notification> result = notifyService.getNotesForUser( userId );
    return ResponseEntity.ok().body( result );


  }

  @PostMapping("/transfer")
  public ResponseEntity<?> postTransfer(@RequestBody TransferDto transferDto){
    notifyService.saveTransferNote( transferDto );
    return ResponseEntity.ok().build();
  }
}