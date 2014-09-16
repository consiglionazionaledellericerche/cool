import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import it.cnr.cool.cmis.service.CMISService


@Component class Sample {
    @Autowired
    private CMISService cmisService

    def getId() {
      cmisService.adminUserId
    }
}

