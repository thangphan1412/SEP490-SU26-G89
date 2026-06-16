import {Button, Form, FormControl, FormGroup, FormLabel} from "react-bootstrap";


function LoginForm(){
    return(
        <div className="container-fluid">
            <Form>
                <FormGroup className="mb-3">
                    <FormLabel >Email Address</FormLabel>
                    <FormControl type="email" placeholder="A@Gmail.com"></FormControl>
                </FormGroup>
                <FormGroup>
                    <FormLabel>Password</FormLabel>
                    <FormControl type="password" placeholder="Length password > 8"></FormControl>
                </FormGroup>
                <Button variant="primary">
                    Enter
                </Button>
            </Form>
        </div>
    )
}
export default LoginForm