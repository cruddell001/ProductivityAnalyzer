package extensions



import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("BigCommerce")

fun bcprint(msg: String) {
    logger.info(msg)
}
