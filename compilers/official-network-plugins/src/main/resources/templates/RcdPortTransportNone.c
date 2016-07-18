/* ******** Start of template RcdPortTransportNone.c */

void /*CFG_CPPNAME_SCOPE*/to_transport_/*PORT_NAME*/( uint8_t *buf_ptr, uint8_t buf_len){
    to_link_/*PORT_NAME*/( buf_ptr, buf_len);
}

void /*CFG_CPPNAME_SCOPE*/from_link_/*PORT_NAME*/( uint8_t *buf_ptr, uint8_t buf_len) {
    from_transport_/*PORT_NAME*/( buf_ptr, buf_len);
}

/* ******** End of template RcdPortTransportNone.c */
