/* ******** Start of template PosixUdpShimAck.c */

static unsigned long long get_system_time_/*PORT_NAME*/(void) {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    unsigned long long millisecondsSinceEpoch =
    (unsigned long long)(tv.tv_sec) * 1000 +
    (unsigned long long)(tv.tv_usec) / 1000;

    return millisecondsSinceEpoch;
end

static void to_shim_/*PORT_NAME*/( unsigned char *buf_ptr, unsigned int buf_len){
    unsigned int wr_idx = /*PORT_NAME*/_instance.tx_buf_wr_idx;
    unsigned int rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
    unsigned char *tx_buf_ptr =  /*PORT_NAME*/_instance.tx_buf;
    unsigned int req_len;
    bool empty_buf = rd_idx == wr_idx;
    
    if( buf_len <= /*PORT_NAME*/_MTU_SIZE ) {
        
        req_len = buf_len + 1; // Request one extra byte to store buf_len
        
        /*PORT_NAME*/_make_room_in_tx_buf(req_len);

        //printf("/*PORT_NAME*/ Put into tx buf len(%d) (%d)\n", buf_len, empty_buf);
        tx_buf_ptr[wr_idx] = buf_len;
        wr_idx = (wr_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        for (int i = 0; i < buf_len; i++) {
            tx_buf_ptr[wr_idx] = buf_ptr[i];
            wr_idx = (wr_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        }
        /*PORT_NAME*/_instance.tx_buf_wr_idx = wr_idx;
    } else {
        printf("/*PORT_NAME*/ Error len(%d) > MTU(%d) ... fragmentation not supported for this transport layer\n", buf_len, /*PORT_NAME*/_MTU_SIZE);
    }
    
    if ( empty_buf ) {
        /*PORT_NAME*/_send_oldest_in_tx_buf();
    } else {
        /*PORT_NAME*/_timeout_check();
    }
}

static void from_transport_/*PORT_NAME*/( unsigned char *rcv_buf_ptr, unsigned int rcv_len){

    unsigned int rx_seq_num = rcv_buf_ptr[0];
    if(rcv_len == 1) {
        // This is an ACK - Seq num is based on our TX
        if(rx_seq_num == /*PORT_NAME*/_instance.tx_seq_num) {
            //printf("/*PORT_NAME*/ RxAck rx(%d) == tx(%d)\n", rx_seq_num, /*PORT_NAME*/_instance.tx_seq_num);
            /*PORT_NAME*/_remove_oldest_from_tx_buf();  // This one is now ack'ed
            /*PORT_NAME*/_instance.tx_seq_num = ( /*PORT_NAME*/_instance.tx_seq_num + 1 ) % /*PORT_NAME*/_SEQ_MOD;
            /*PORT_NAME*/_send_oldest_in_tx_buf(); // Send out next if any pending
        } else {
            printf("/*PORT_NAME*/ RxAck rx(%d) != tx(%d)\n", rx_seq_num, /*PORT_NAME*/_instance.tx_seq_num);
            // TODO Error handling
        }
    } else {
        // This is a MSG - Seq num is chosen by remote
        //printf("/*PORT_NAME*/ RxData len(%d) seq(%d)\n", rcv_len, rx_seq_num);

        // Send ACK to remote ...
        unsigned char tmp_buf[1];
        tmp_buf[0] = rx_seq_num;
        to_transport_/*PORT_NAME*/( tmp_buf, 1);

        if((rx_seq_num == 0) || (rx_seq_num == /*PORT_NAME*/_instance.rx_seq_num + 1)) {
            /*PORT_NAME*/_instance.rx_seq_num = rx_seq_num;
            /*PORT_NAME*/_parser( &(rcv_buf_ptr[1]), rcv_len-1);
        } else {
              printf("/*PORT_NAME*/ RxData rx(%d) != expected_rx(%d)\n", rx_seq_num, /*PORT_NAME*/_instance.rx_seq_num + 1);
        }
    }
}

static void /*PORT_NAME*/_timeout_check(void){
    unsigned long long timeout_ms = /*PORT_NAME*/_TIMEOUT_MS;
    if (timeout_ms == 0) timeout_ms = 1;
        
    timeout_ms = /*PORT_NAME*/_instance.last_sent_ms + timeout_ms;
    if ( get_system_time_/*PORT_NAME*/() > timeout_ms ) {
        // Try to resend to retry ack
        printf("/*PORT_NAME*/ Try timeout\n");
        /*PORT_NAME*/_send_oldest_in_tx_buf();
    }
}

static unsigned int /*PORT_NAME*/_remove_oldest_from_tx_buf(void){
    unsigned int rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
    unsigned int buf_len = /*PORT_NAME*/_instance.tx_buf[rd_idx];
    int i;
    
    if (/*PORT_NAME*/_instance.tx_buf_rd_idx != /*PORT_NAME*/_instance.tx_buf_wr_idx) {
        rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        for (i = 0; i < buf_len; i++) {
            rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        }
        /*PORT_NAME*/_instance.tx_buf_rd_idx = rd_idx;
    }
    return rd_idx;
}

static void /*PORT_NAME*/_make_room_in_tx_buf( unsigned int req_len){
    bool room_in_tx_buf;
    unsigned int rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
    unsigned int wr_idx = /*PORT_NAME*/_instance.tx_buf_wr_idx;
    
    while(1) {
        room_in_tx_buf = true;
        
        // Check if it is room
        if(wr_idx > rd_idx) {
            if(((wr_idx + req_len) >= /*PORT_NAME*/_TX_BUF_SIZE) &&
               (((wr_idx + req_len) - /*PORT_NAME*/_TX_BUF_SIZE) >= rd_idx)) {
                room_in_tx_buf = false;        // No room
            }
        }
        else if(wr_idx < rd_idx) {
            if((wr_idx + req_len) >= rd_idx){
                room_in_tx_buf = false;        // No room
            }
        }
        
        if( room_in_tx_buf ) break; // It is room ... skip the loop
        
        // It is not room, remove oldest from buf
        rd_idx = /*PORT_NAME*/_remove_oldest_from_tx_buf();

        printf("/*PORT_NAME*/ Buffer overflow oldest message skipped\n");
    } 
}

static void /*PORT_NAME*/_send_oldest_in_tx_buf(void){
    unsigned char tmp_buf[/*PORT_NAME*/_MTU_SIZE];
    if( /*PORT_NAME*/_instance.tx_buf_rd_idx != /*PORT_NAME*/_instance.tx_buf_wr_idx){
        unsigned int rd_idx = /*PORT_NAME*/_instance.tx_buf_rd_idx;
        unsigned int *tx_buf_ptr =  /*PORT_NAME*/_instance.tx_buf;
        unsigned int buf_len;

        tmp_buf[0] = /*PORT_NAME*/_instance.tx_seq_num;

        buf_len = tx_buf_ptr[rd_idx];
        rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        for (int i = 0; i < buf_len; i++) {
            tmp_buf[i+1] = tx_buf_ptr[rd_idx];
            rd_idx = (rd_idx + 1) % /*PORT_NAME*/_TX_BUF_SIZE;
        }

        //printf("/*PORT_NAME*/ Send oldest len(%d) seq(%d)\n", buf_len, /*PORT_NAME*/_instance.tx_seq_num);

        /*PORT_NAME*/_forwardMessage( tmp_buf, buf_len+1);
        /*PORT_NAME*/_instance.last_sent_ms = get_system_time_/*PORT_NAME*/();
    }
}

/* ******** End of template PosixUdpShimAck.c */
