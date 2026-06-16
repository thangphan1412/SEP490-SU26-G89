import {Button, Form, FormControl, FormGroup, FormLabel} from "react-bootstrap";

function FogotPassword(){
    return(
        <div className="forgot-container">
            <h4>Quen mat khau</h4>
            <div className="form-input-forgot">
                <Form className="mb-3">
                    <FormGroup>
                        <FormLabel>Gmail</FormLabel>
                        <FormControl type="gmail" placeholder="Nhập địa chỉ gmail"></FormControl>
                    </FormGroup>
                    <Button>Hủy bỏ</Button>
                   <Button className="m-md-5">Xác nhận</Button>
                </Form>
            </div>
        </div>
    )
}
 export default FogotPassword