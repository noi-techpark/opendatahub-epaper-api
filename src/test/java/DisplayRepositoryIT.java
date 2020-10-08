import it.noi.edisplay.MainApplicationClass;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.repositories.DisplayRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes = MainApplicationClass.class)
@RunWith(SpringRunner.class)
@DataJpaTest
public class DisplayRepositoryIT {

    @Autowired
    private DisplayRepository displayRepository;

    @Test
    public void whenFindByUuidThenReturnDisplay() {
        // given
        String name = "findByUUidTes";
        String uuid = UUID.randomUUID().toString();
        Date date = new Date();


        Display display = new Display();
        display.setName(name);
        display.setUuid(uuid);
        display.setLastState(date);


        displayRepository.save(display);

        // when
        Display foundDisplay = displayRepository.findByUuid(display.getUuid());

        // then
        assertDisplay(date, display, foundDisplay);
    }

    @Test
    public void crudTest() {
        // given
        String name = "CrudTest";
        String uuid = UUID.randomUUID().toString();
        Date date = new Date();


        Display display = new Display();
        display.setName(name);
        display.setUuid(uuid);
        display.setLastState(date);


        //create
        Display createdDisplay = displayRepository.save(display);

        //read
        Display foundDisplay = displayRepository.getOne(createdDisplay.getId());
        assertDisplay(date, display, foundDisplay);

        //update
        // given
        String updateName = "UpdatedCrudTest";
        Date updateLastState = new Date();

        foundDisplay.setName(updateName);
        foundDisplay.setLastState(updateLastState);

        displayRepository.save(foundDisplay);

        Display updatedDisplay = displayRepository.getOne(createdDisplay.getId());
        assertDisplay(updateLastState, foundDisplay, updatedDisplay);

        //delete
        displayRepository.delete(display);
        Display deletedDisplay;

        deletedDisplay = displayRepository.findByUuid(display.getUuid());
        assertThat(deletedDisplay).isNull();


    }

    private void assertDisplay(Date date, Display display, Display foundDisplay) {
        assertThat(foundDisplay).isNotNull();
        assertThat(foundDisplay.getUuid())
                .isEqualTo(display.getUuid());
        assertThat(foundDisplay.getName())
                .isEqualTo(display.getName());
        assertThat(foundDisplay.getLastState())
                .isEqualTo(date);
        assertThat(foundDisplay.getCreated()).isNotNull();
        assertThat(foundDisplay.getLastUpdate()).isNotNull();
    }
}
