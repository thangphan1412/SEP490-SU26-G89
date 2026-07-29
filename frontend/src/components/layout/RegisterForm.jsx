import {Button, Form, FormControl, FormGroup, FormLabel} from "react-bootstrap";
function RegisterForm(){
    return(
        <div className="container-fluid">
            <Form>
                <FormGroup className="mb-3">
                    <FormLabel>Mã số thuế </FormLabel>
                    <FormControl type="number" placeholder="Nhập mã số thuế"></FormControl>
                </FormGroup>
                <FormGroup>
                    <FormLabel>Email</FormLabel>
                    <FormControl type="email" placeholder="A@gmail.com"></FormControl>
                </FormGroup>
                <Button>Gửi OTP</Button>
            </Form>
        </div>
    )
}
export default RegisterForm