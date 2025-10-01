package javamid.notify.service;

import javamid.notify.model.TransferDto;
import javamid.notify.repository.NotifyRepository;
import org.springframework.stereotype.Service;
import javamid.notify.model.Notification;

import java.util.List;

@Service
public class NotifyService {

  private final NotifyRepository notifyRepository;
  public NotifyService( NotifyRepository notifyRepository ){ this.notifyRepository = notifyRepository; }

  public void   saveTransferNote(TransferDto transferDto){
    String message = "Пользователь " + transferDto.getUserIdFrom() + " перевел вам " +
            transferDto.getValueTo() + " " + transferDto.getCurrencyTo() + "." ;
    Notification notification = new Notification( transferDto.getUserIdTo(), message );
    notifyRepository.save( notification );
  }

  public List<Notification> getNotesForUser(Long userId) {
    return notifyRepository.findByUserIdAndReadIsFalse( userId );
  }

  public void markAsRead( Long id ){
    Notification notification = notifyRepository.getById( id );
    notification.setRead( true );
    notifyRepository.save( notification );
  }
}
